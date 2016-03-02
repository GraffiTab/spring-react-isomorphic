/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.isomorphic;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
public class CommentController {

	private CommentRepository commentRepository;
	private List<SseEmitter> sseEmitters = Collections.synchronizedList(new ArrayList<>());
	private final MockDataService mockDataService;

	@Autowired
	CommentController(CommentRepository commentRepository, MockDataService mockDataService) {
		this.commentRepository = commentRepository;
		this.mockDataService = mockDataService;
	}

	@RequestMapping(path = "/", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	Iterable<Comment> jsonFindAll() {
		return this.commentRepository.findAll();
	}

	@RequestMapping(path = "/", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	Comment jsonCreate(Comment comment) throws IOException {
		Comment newComment = this.commentRepository.save(comment);
		synchronized (this.sseEmitters) {
			for (SseEmitter sseEmitter : this.sseEmitters) {
				// Servlet containers don't always detect ghost connection, so we must catch exceptions ...
				try {
					sseEmitter.send(newComment, MediaType.APPLICATION_JSON);
				} catch (Exception e) {}
			}
		}
		return comment;
	}

	@RequestMapping("/")
	String render(Model model) {
		model.addAttribute("title", "Layout example");
		model.addAttribute("comments", this.commentRepository.findAll());
		return "index";
	}

	@RequestMapping("/p/{productId}")
	public ModelAndView getPDP(@PathVariable String productId) {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("pdp");

		JsonNode configData = mockDataService.getMockJson("config");
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> labels = mapper.convertValue(configData.get("labels"), Map.class);
		Map<String, String> config = mapper.convertValue(configData.get("config"), Map.class);

		JsonNode globalHeader = mockDataService.getMockJson("globalheader");

		Map<String, String> globalData = mapper.convertValue(globalHeader, Map.class);

		JsonNode product = mockDataService.getMockJson("products/" + productId);


		modelAndView.addObject("labels", labels);
		modelAndView.addObject("config", config);
		modelAndView.addObject("globalHeader", globalData);
		modelAndView.addObject("pageContent", new HashMap() {{
			put("title", product.get("name"));
		}});
		modelAndView.addObject("MS_RES_PDP", mapper.convertValue(product, Map.class));
		modelAndView.addObject("MS_RES_PDP_MAPPER", product.toString());
		return modelAndView;
	}

	@RequestMapping("/sse/updates")
	SseEmitter subscribeUpdates() {
		SseEmitter sseEmitter = new SseEmitter();
		synchronized (this.sseEmitters) {
			this.sseEmitters.add(sseEmitter);
			sseEmitter.onCompletion(() -> {
				synchronized (this.sseEmitters) {
					this.sseEmitters.remove(sseEmitter);
				}
			});
		}
		return sseEmitter;
	}

}
