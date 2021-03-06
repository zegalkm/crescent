<%@page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core"  %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html lang="en">
<%@ include file="../common/header.jsp"%>
<script>
				function enterFind(e) {
					if (e.keyCode == 13)
						findWord();
					return true;
				};
				function enterMorph(e){
					if (e.keyCode == 13)
						morphTest();
					return true;
				}
				function enterAdd(e){
					if(e.keyCode == 13)
						addWord();
					return true;
				}
                function prev() {
                        $('#pagingAction').val("prev");
                        $('#dictionaryForm').submit();

                }

                function next() {
                        $('#pagingAction').val("next");
                        $('#dictionaryForm').submit();

                }

                function addWord() {
                		if($('#wordToAdd').val() == '') {
                			newAlert('검색어를 입력해주세요.');
                			return;
                		}
                        $('#dictionaryForm').attr('action', 'dictionaryManageAdd.devys').submit();
                }

                function removeWord() {
                        var checked = [];
                        $("input[name='wordToRemove[]']:checked").each(function ()
                        {
                            checked.push($(this).val());
                        });

                        $('#wordsToRemove').val(checked);

                        $('#dictionaryForm').attr('action', 'dictionaryManageRemove.devys').submit();
                }

                function findWord() {
                $('#dictionaryForm').attr('action', 'dictionaryManageFind.devys').submit();
                }

                /* function morphTest() {
                        var param = {keyword:$('#wordForTest').val()};
                        $.post("/doMorphTestAjax.devys", param, function(data){
                                $('#morphResult').text("");
                                $('#morphResult').text(data);
                        });
                } */

                function morphTest() {
                        var param = {keyword:$('#wordForTest').val()};

                        $.getJSON('doMorphTestAjax.devys', param, function(data) {

                                  var indexingResult = '';
                                  for(var i = 0; i < data.indexResult.length; i++) {
                                          if(indexingResult.length > 0) {
                                                  indexingResult = indexingResult + ', ';
                                          }
                                          indexingResult = indexingResult + ' [' + data.indexResult[i].word + '(' + data.indexResult[i].type;
                                          indexingResult = indexingResult + ', ' + data.indexResult[i].startOffset + ',' + data.indexResult[i].endOffset + ')]';
                                  }

                                  var queryResult = '';
                                  for(var i = 0; i < data.queryResult.length; i++) {
                                          if(queryResult.length > 0) {
                                                  queryResult = queryResult + ', ';
                                          }
                                          queryResult = queryResult + ' [' + data.queryResult[i].word + '(' + data.queryResult[i].type;
                                          queryResult = queryResult + ', ' + data.queryResult[i].startOffset + ',' + data.queryResult[i].endOffset + ')]';
                                  }

                                  var result = '<ul>';
                                  result += '<li>'+'인덱싱 : ' + indexingResult+'</li>';
                                  result += '<li>'+'쿼리 : ' + queryResult+'</li>';
                                  result += '</ul>';

                                  $('#morphResult').html(result);
                        });
                }
        </script>
<body>
	<%@ include file="../common/menu.jsp"%>
	<div>
		<div>
			<ul class="nav nav-tabs">
				<li <c:if test='${dicType eq "noun"}'>class="active"</c:if>><a href="dictionaryManage.devys?dicType=noun">명사사전</a></li>
				<li <c:if test='${dicType eq "stop"}'>class="active"</c:if>><a href="dictionaryManage.devys?dicType=stop">불용어사전</a></li>
				<li <c:if test='${dicType eq "syn"}'>class="active"</c:if>><a href="dictionaryManage.devys?dicType=syn">동의어사전</a></li>
				<li <c:if test='${dicType eq "compuond"}'>class="active"</c:if>><a href="dictionaryManage.devys?dicType=compound">복합명사사전</a></li>
			</ul>
		</div>
		<form class="form-horizontal" id="dictionaryForm" method="post" action="dictionaryManage.devys">
			<input type="hidden" id="dicType" name="dicType" value="${dicType}" /> 
			<input type="hidden" id="startOffset" name="startOffset" value="${startOffset}" /> 
			<input type="hidden" id="pagingAction" name="pagingAction" value="" /> 
			<input 	type="hidden" id="wordsToRemove" name="wordsToRemove" value="" />
			<div class="container">
				<table class="table table-bordered">
					<thead>
						<tr>
							<th width="30px">선택</th>
							<th style="text-align:center;">단어</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="word" items="${dictionary}">
							<tr>
			            		<td><input type="checkbox" id="${word}" name="wordToRemove[]" value="${word}" /></td>
								<td style="text-align:center;">${word}</td>
			            	</tr>
			            </c:forEach>
					</tbody>
				</table>
			</div>
			<div class="form-inline">
				<p class="text-info">
					단어개수
					${dictionarySize}개
				</p>
				<a href="javascript:removeWord();" class="btn  btn-small btn-danger">선택단어삭제</a>
				<a href="adminMain.devys" class="btn  btn-small btn-success">관리자
					메인으로</a>
			</div>
			<div class="control-group" style="padding-top:20px;">
				<label class="control-label" for="wordToAdd">단어추가</label>
				<div class="controls">
					<input type="text" id="wordToAdd" name="wordToAdd" onkeypress="enterAdd(event);" value=""
						placeholder="추가할 단어를 입력해주세요." class="input-large search-query" />
					<button class="btn btn-small btn-primary" type="button"
						onclick="javascript:addWord();">단어추가</button>
				</div>
				<div id="alert-area"></div>
			</div>
			<div class="control-group">
				<label class="control-label" for="wordToFind">단어찾기</label>
				<div class="controls">
					<input type="text" id="wordToFind" name="wordToFind" onkeypress="enterFind(event);" value=""
						placeholder="찾을 단어를 입력해주세요." class="input-large search-query" />
					<button class="btn btn-small btn-primary" type="button"
						onclick="javascript:findWord();">단어찾기</button>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label" for="wordToFind">형태소분석 테스트하기</label>
				<div class="controls">

					<input type="text" id="wordForTest" name="wordForTest" onkeypress="enterMorph(event)" value=""
						placeholder="테스트할 단어를 입력해주세요." class="input-large search-query" />
					<button class="btn btn-small btn-primary" type="button"
						onclick="javascript:morphTest();">형태소분석 테스트하기</button>

				</div>
			</div>

			<div id="morphResult"></div>
			<c:if test="${dictionarySize gt 20}">
				<ul class="pager">
					<li><a href="javascript:prev();">Previous</a></li>
					<li><a href="javascript:next();">Next</a></li>
				</ul>
			</c:if>
		</form>
	</div>
</body>
</html>
