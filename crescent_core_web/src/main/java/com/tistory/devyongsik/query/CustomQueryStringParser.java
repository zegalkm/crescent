package com.tistory.devyongsik.query;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tistory.devyongsik.domain.CrescentCollectionField;
import com.tistory.devyongsik.exception.CrescentInvalidRequestException;

public class CustomQueryStringParser {

	private Logger logger = LoggerFactory.getLogger(CustomQueryStringParser.class);
	private static Pattern pattern = Pattern.compile("(.*?)(:)(\".*?\")");
	private Query resultQuery = null;
	
	protected Query getQuery(List<CrescentCollectionField> indexedFields, String customQueryString, Analyzer analyzer) throws CrescentInvalidRequestException {
		if(resultQuery != null) {
			return this.resultQuery;
		} else {
			return getQueryFromCustomQuery(indexedFields, customQueryString, analyzer);
		}
	}
	
	private Query getQueryFromCustomQuery(List<CrescentCollectionField> indexedFields, String customQueryString, Analyzer analyzer) 
			throws CrescentInvalidRequestException {
		
		//패턴분석
		Matcher m = pattern.matcher(customQueryString);
		
		String fieldName = "";
		Occur occur = Occur.SHOULD;
		String userRequestQuery = "";
		float boost = 0F;
		
		boolean isRangeQuery = false;
		BooleanQuery resultQuery = new BooleanQuery();
		
		CrescentCollectionField searchTargetField = null;
		
		while(m.find()) {
			if(m.groupCount() != 3) {
				throw new CrescentInvalidRequestException("쿼리 문법 오류. [" + customQueryString + "]");
			}
			
			fieldName = m.group(1).trim();
			if(fieldName.startsWith("-")) {
				occur = Occur.MUST_NOT;
				fieldName = fieldName.substring(1);
			} else if (fieldName.startsWith("+")) {
				occur = Occur.MUST;
				fieldName = fieldName.substring(1);
			}
			
			//field가 검색 대상에 있는지 확인..
			boolean any = true;
			boolean isLongField = false;
			boolean isAnalyzed = false;
			for(CrescentCollectionField crescentField : indexedFields) {
				if(fieldName.equals(crescentField.getName())) {
					any = false;
					searchTargetField = crescentField;
					
					isLongField = "LONG".equals(crescentField.getType());
					isAnalyzed = crescentField.isAnalyze();
					
					logger.debug("selected searchTargetField : {} ", searchTargetField);
					break;
				}
			}
			
			if(any) {
				logger.error("검색 할 수 없는 필드입니다. {} " , fieldName);
				throw new CrescentInvalidRequestException("검색 할 수 없는 필드입니다. [" + fieldName + "]");
			}
			
			
			userRequestQuery = m.group(3).trim().replaceAll("\"", "");
			if((userRequestQuery.startsWith("[") && userRequestQuery.endsWith("]")) 
					|| (userRequestQuery.startsWith("{") && userRequestQuery.endsWith("}"))) {
				
				isRangeQuery = true;
			
			}
			
			//boost 정보 추출
			int indexOfBoostSign = userRequestQuery.indexOf("^");
			if(indexOfBoostSign >= 0) {
				boost = Float.parseFloat(userRequestQuery.substring(indexOfBoostSign+1));
				userRequestQuery = userRequestQuery.substring(0, indexOfBoostSign);
			}
			
			logger.info("user Request Query : {} ", userRequestQuery);
			logger.info("boost : {} ", boost);
			
			//range쿼리인 경우에는 RangeQuery 생성
			if(isRangeQuery) {
	
				//QueryParser qp = new QueryParser(Version.LUCENE_36, fieldName, analyzer);
				String minValue = "";
				String maxValue = "";
				boolean isIncludeMin = false;
				boolean isIncludeMax = false;
				
				String[] splitQuery = userRequestQuery.split("TO");
				logger.info("splitQuery : {}", Arrays.toString(splitQuery));
				
				if(splitQuery.length != 2) {
					logger.error("문법 오류 확인바랍니다. {} " , userRequestQuery);
					throw new CrescentInvalidRequestException("문법 오류 확인바랍니다. [" + userRequestQuery + "]");
				}
				
				if(splitQuery[0].trim().startsWith("[")) {
					isIncludeMin = true;
				}
				
				if(splitQuery[1].trim().endsWith("]")) {
					isIncludeMax = true;
				}
				
				logger.info("minInclude : {}, maxInclude : {}", isIncludeMin, isIncludeMax);
				
				minValue = splitQuery[0].trim().substring(1);
				maxValue = splitQuery[1].trim().substring(0, splitQuery[1].trim().length() - 1);
				
				logger.info("minValue : {}, maxValue : {}", minValue, maxValue);
				
				boolean isNumeric = false;
				isNumeric = StringUtils.isNumeric(minValue) && StringUtils.isNumeric(maxValue);
				
				logger.info("isLongField : {}", isLongField);
				logger.info("is numeric : {}", isNumeric);
				
				Query query = null;
				
				if(isAnalyzed) {
					logger.error("범위검색 대상 field는 analyzed값이 false이어야 합니다. {} " , userRequestQuery);
					throw new CrescentInvalidRequestException("범위검색 대상 field는 analyzed값이 false이어야 합니다. [" + userRequestQuery + "]");
				}
				if(isLongField && isNumeric) {
					query = NumericRangeQuery.newLongRange(fieldName, Long.parseLong(minValue), Long.parseLong(maxValue), isIncludeMin, isIncludeMax);
				} else if (!(isLongField && isNumeric)){
					query = new TermRangeQuery(fieldName, minValue, maxValue, isIncludeMin, isIncludeMax);
				} else {
					logger.error("범위검색은 필드의 타입과 쿼리의 타입이 맞아야 합니다. {} " , userRequestQuery);
					throw new CrescentInvalidRequestException("범위검색은 필드의 타입과 쿼리의 타입이 맞아야 합니다. [" + userRequestQuery + "]");
				}
				
				resultQuery.add(query, occur);
				
			} else {
				//쿼리 생성..
				String[] keywords = userRequestQuery.split( " " );
				
				if(logger.isDebugEnabled()) {
					logger.info("split keyword : {}", Arrays.toString(keywords));
				}
				
				for(int i = 0; i < keywords.length; i++) {
					ArrayList<String> analyzedTokenList = analyzedTokenList(analyzer, keywords[i]);

					if(analyzedTokenList.size() == 0) {
						
						Term t = new Term(fieldName, keywords[i]);
						Query query = new TermQuery(t);
						
						if(searchTargetField.getBoost() > 1F && boost > 1F) {
							query.setBoost(searchTargetField.getBoost() + boost);
						} else if (boost > 1F) {
							query.setBoost(boost);
						} else if (searchTargetField.getBoost() > 1F) {
							query.setBoost(searchTargetField.getBoost());
						}
						
						resultQuery.add(query, occur);
						
						logger.debug("query : {} ", query.toString());
						logger.debug("result query : {} ", resultQuery.toString());
						
					} else {
						
						for(String str : analyzedTokenList) {
							
							Term t = new Term(fieldName, str);
							Query query = new TermQuery(t);
							
							if(searchTargetField.getBoost() > 1F && boost > 1F) {
								query.setBoost(searchTargetField.getBoost() + boost);
							} else if (boost > 1F) {
								query.setBoost(boost);
							} else if (searchTargetField.getBoost() > 1F) {
								query.setBoost(searchTargetField.getBoost());
							}
							
							resultQuery.add(query, occur);
							
							logger.debug("query : {} ", query.toString());
							logger.debug("result query : {} ", resultQuery.toString());
						}
					}
				}
			}
		}
		
		this.resultQuery = resultQuery;
		
		return resultQuery;
	}
	
	private ArrayList<String> analyzedTokenList(Analyzer analyzer, String splitedKeyword) {
		Logger logger = LoggerFactory.getLogger(DefaultKeywordParser.class);
		
		ArrayList<String> rst = new ArrayList<String>();
		//split된 검색어를 Analyze..
		TokenStream stream = analyzer.tokenStream("", new StringReader(splitedKeyword));
		CharTermAttribute charTerm = stream.getAttribute(CharTermAttribute.class);
		

		try {
			stream.reset();
			
			while(stream.incrementToken()) {
				rst.add(charTerm.toString());
			}
			
		} catch (IOException e) {
			logger.error("error in DefaultKeywordParser : ", e);
			throw new RuntimeException(e);
		}

		logger.debug("[{}] 에서 추출된 명사 : [{}]", new String[]{splitedKeyword, rst.toString()});
			

		return rst;
	}
}
