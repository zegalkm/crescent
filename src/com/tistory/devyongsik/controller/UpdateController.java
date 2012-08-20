package com.tistory.devyongsik.controller;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tistory.devyongsik.config.CollectionConfig;
import com.tistory.devyongsik.handler.Handler;
import com.tistory.devyongsik.handler.JsonDataHandler;
import com.tistory.devyongsik.index.FullmoonIndexExecutor;

/**
 * author : need4spd, need4spd@naver.com, 2012. 8. 15.
 */

@Controller
public class UpdateController {
	
	Logger logger = LoggerFactory.getLogger(UpdateController.class);
	
	@RequestMapping("/update")
	public String updateDocument(HttpServletRequest request, HttpServletResponse response) {
		
		String contentsType = request.getHeader("Content-type");
		
		//TODO contentsType별로 핸들러 분리
		//TODO 일단 json만..
		Handler handler = null;
		if("application/json".equals(contentsType)) {
			handler = new JsonDataHandler();
		}
		
		StringBuilder text = new StringBuilder();
		try {
			
			BufferedReader reader = request.getReader();
			String tmp = "";
			while((tmp = reader.readLine()) != null) {
				text.append(tmp);
				logger.info(tmp);
			}
			
			reader.close();
			
		} catch (IOException e) {
			logger.error("error : ", e);
		}
		
		FullmoonIndexExecutor excutor = new FullmoonIndexExecutor(CollectionConfig.getInstance().getCollection("sample"), handler);
		excutor.execute(text.toString());
		
		return null;
	}
}
