/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.model.dao.search.hibernate;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jtalks.jcommune.model.ObjectsFactory;
import org.jtalks.jcommune.model.dao.search.hibernate.filter.SearchRequestFilter;
import org.jtalks.jcommune.model.entity.Topic;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * 
 * @author Anuar Nurmakanov
 * 
 */
@ContextConfiguration(locations = { "classpath:/org/jtalks/jcommune/model/entity/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TopicHibernateSearchDaoTest extends AbstractTransactionalTestNGSpringContextTests {
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private TopicHibernateSearchDao topicSearchDao;
	@Mock
	private SearchRequestFilter invalidCharactersFilter;
	@Mock
	private SearchRequestFilter stopWordsFilter;
	
	private FullTextSession fullTextSession;
	
	@BeforeMethod
	public void init() {
		Session session = sessionFactory.getCurrentSession();
        ObjectsFactory.setSession(session);
        
        MockitoAnnotations.initMocks(this);
        List<SearchRequestFilter> filters = Arrays.asList(invalidCharactersFilter, stopWordsFilter);
        topicSearchDao.setFilters(filters);
	}
	
	private void configureMocks(String searchText, String result) {
		Mockito.when(invalidCharactersFilter.filter(searchText)).thenReturn(result);
		Mockito.when(stopWordsFilter.filter(searchText)).thenReturn(result);
	}
	
	@BeforeMethod
	public void initHibernateSearch() throws InterruptedException {
		Session session = sessionFactory.getCurrentSession();
		fullTextSession = Search.getFullTextSession(session);
		fullTextSession.createIndexer().startAndWait();
	}
	
	@AfterMethod
	public void clearIndexes() {
		fullTextSession.purgeAll(Topic.class);
		fullTextSession.flushToIndexes();
	}
	
	@Test
	public void testSearchWithFullyDirtySearchText() {
		configureMocks(StringUtils.EMPTY, StringUtils.EMPTY);
		
		List<Topic> searchResults = topicSearchDao.searchByTitleAndContent(StringUtils.EMPTY);
		
		Assert.assertTrue(searchResults.size() == 0, "Search result must be empty.");
	}
	
	@Test(dataProvider = "parameterFullPhraseSearch")
	public void testFullPhraseSearch(String content) {
		Topic expectedTopic = ObjectsFactory.getDefaultTopic();
		expectedTopic.setTitle(content);
		
		saveAndFlushIndexes(Arrays.asList(expectedTopic));
		configureMocks(content, content);
		
		List<Topic> searchResult = topicSearchDao.searchByTitleAndContent(content);
		
		Assert.assertTrue(searchResult != null, "Search result must not be null.");
		Assert.assertTrue(searchResult.size() != 0, "Search result must not be empty.");
		for (Topic topic : searchResult) {
			Assert.assertEquals(expectedTopic.getTitle(), topic.getTitle(), 
					"Content from the index should be the same as in the database.");
		}
	}
	
	@Test(dataProvider = "parameterFullPhraseSearch")
	public void testPostContentSearch(String content) {
	    Topic expectedTopic = ObjectsFactory.getDefaultTopic();
	    expectedTopic.getLastPost().setPostContent(content);
	    
	    saveAndFlushIndexes(Arrays.asList(expectedTopic));
        configureMocks(content, content);
	    
	    List<Topic> searchResult = topicSearchDao.searchByTitleAndContent(content);
        
        Assert.assertTrue(searchResult != null, "Search result must not be null.");
        Assert.assertTrue(searchResult.size() != 0, "Search result must not be empty.");
        for (Topic topic : searchResult) {
            Assert.assertEquals(expectedTopic.getTitle(), topic.getTitle(), 
                    "Content from the index should be the same as in the database.");
        }
	}
	
	@DataProvider(name = "parameterFullPhraseSearch")
	public Object[][] parameterFullPhraseSearch() {
		return new Object[][] {
				{"Содержимое темы."},
				{"Topic content."}
		};
	}
	
	@Test(dataProvider = "parameterPiecePhraseSearch")
	public void testPiecePhraseSearch(String firstPiece, char delimeter, String secondPiece){
		String content = new StringBuilder().
				append(firstPiece).
				append(delimeter).
				append(secondPiece).
				toString();
		
		Topic expectedTopic = ObjectsFactory.getDefaultTopic();
        expectedTopic.setTitle(content);
		
		saveAndFlushIndexes(Arrays.asList(expectedTopic));
		
		for (String piece: Arrays.asList(firstPiece, secondPiece)) {
			configureMocks(piece, piece);
			
			List<Topic> searchResults = topicSearchDao.searchByTitleAndContent(piece);
			
			Assert.assertTrue(searchResults != null, "Search result must not be null.");
			Assert.assertTrue(searchResults.size() != 0, "Search result must not be empty.");
		}
	}
	
	@DataProvider(name = "parameterPiecePhraseSearch")
	public Object[][] parameterPiecePhraseSearch() {
		return new Object[][] {
				{"Содержимое", ' ',  "топика"},
				{"Topic", ' ', "content"}
		};
	}
	
	@Test(dataProvider = "parameterIncorrectPhraseSearch")
	public void testIncorrectPhraseSearch(String correct, String incorrect) {
		Topic expectedTopic = ObjectsFactory.getDefaultTopic();
        expectedTopic.setTitle(correct);
		
		saveAndFlushIndexes(Arrays.asList(expectedTopic));
		configureMocks(incorrect, incorrect);
		
		List<Topic> searchResults = topicSearchDao.searchByTitleAndContent(incorrect);
		
		Assert.assertTrue(searchResults != null, "Search result must not be null.");
		Assert.assertTrue(searchResults.size() == 0, "Search result must be empty.");
	}
	
    private <E> void saveAndFlushIndexes(List<E> entityList) {
		for (E entity : entityList) {
			fullTextSession.save(entity);
		}
		fullTextSession.flushToIndexes();
	}
	
	@DataProvider(name = "parameterIncorrectPhraseSearch")
	public Object[][] parameterIncorrectPhraseSearch() {
		return new Object[][] {
				{"Содержимое поста", "Железный человек"},
				{"Post content", "Iron Man"}
		};
	}
	
	@Test(dataProvider = "parameterSearchByRoot")
	public void testSearchByRoot(String word, String wordWithSameRoot) {
	    Topic expectedTopic = ObjectsFactory.getDefaultTopic();
        expectedTopic.setTitle(word);
		
		saveAndFlushIndexes(Arrays.asList(expectedTopic));
		configureMocks(wordWithSameRoot, wordWithSameRoot);
		
		List<Topic> searchResults = topicSearchDao.searchByTitleAndContent(wordWithSameRoot);
		Assert.assertTrue(searchResults.size() != 0, "Search result must not be empty.");
	}
	
	@DataProvider(name = "parameterSearchByRoot")
	public Object[][] parameterSearchByRoot() {
		return new Object[][] {
				{"Keys", "Key"},
				{"Key", "Keys"},
				{"testing", "Tests"},
				{"tests", "TeStIng"},
				{"Полеты", "полет"},
				{"барабан", "барабаны"}
		};
	}
	
	@Test(dataProvider = "parameterSearchByBbCodes")
	public void testSearchByBbCodes(String content, String bbCode) {
	    Topic expectedTopic = ObjectsFactory.getDefaultTopic();
        expectedTopic.getLastPost().setPostContent(content);
        
        saveAndFlushIndexes(Arrays.asList(expectedTopic));
        configureMocks(bbCode, bbCode);
        
        List<Topic> searchResults = topicSearchDao.searchByTitleAndContent(bbCode);
        Assert.assertTrue(searchResults.size() == 0, "Search result must be empty.");
	}
	
	@DataProvider(name = "parameterSearchByBbCodes")
	public Object[][] parameterSearchByBbCodes() {
	    return new Object[][] {
                {"[code=java]spring[/code]", "code"},
                {"[b]gwt[/b]", "b"}
        };
	}
	
	@Test(dataProvider = "parameterSearchByBbCodesContent")
	public void testSearchByBbCodesContent(String content, String bbCodeContent) {
	    Topic expectedTopic = ObjectsFactory.getDefaultTopic();
        expectedTopic.getLastPost().setPostContent(content);
        
        saveAndFlushIndexes(Arrays.asList(expectedTopic));
        configureMocks(bbCodeContent, bbCodeContent);
        
        List<Topic> searchResults = topicSearchDao.searchByTitleAndContent(bbCodeContent);
        Assert.assertTrue(searchResults.size() != 0, "Search result must not be empty.");
	}
	
	@DataProvider(name = "parameterSearchByBbCodesContent")
	public Object[][] parameterSearchByBbCodesContent() {
	    return new Object[][] {
                {"[code=java]code[/code]", "code"},
                {"[b]b[/b]", "b"}
        };
	}
}