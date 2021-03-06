package com.tistory.devyongsik.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class SampleDataMaker {
	public static void main(String[] args) {
		
		List<Map<String, String>> sampleList = new ArrayList<Map<String, String>>();
		String field_name_1 = "wiki_idx";
		String field_name_2 = "space_idx";
		String field_name_3 = "wiki_title";
		String field_name_4 = "wiki_text";
		String field_name_5 = "ins_user";
		String field_name_6 = "ins_date";
		
		for(int i = 0; i < 500; i++) {
			Map<String, String> doc = new HashMap<String, String>();
			doc.put(field_name_1, String.valueOf(i));
			doc.put(field_name_4, "본문 입니다."+i);
			doc.put(field_name_3, "제목 입니다"+i);
			doc.put(field_name_2, String.valueOf(i/100));
			doc.put(field_name_5, "need4spd");
			doc.put(field_name_6, "20120819");
			
			sampleList.add(doc);
		}
		
		Gson gson = new Gson();
		String sampleData = gson.toJson(sampleList);
		
		System.out.println(sampleData);
	}
}
