package com.flynnovations.game.server;

import java.io.Serializable;
import java.util.*;

import com.flynnovations.game.shared.Question;

public class MockData implements IData, Serializable {
	
	private static final long serialVersionUID = 1L;
	private ArrayList<Question> allQuestions;

	/**
	 * Create MockData class used for testing
	 */
	public MockData() {
		allQuestions = new ArrayList<>();
		allQuestions.add(new Question("easy", "1+1", "2", "3", "4","bananas", "math"));
	}
	
	/** 
	 * Get a mock question
	 */
	@Override
	public Question getQuestion() {
		return allQuestions.get(0);
	}

}
