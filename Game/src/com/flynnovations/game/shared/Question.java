package com.flynnovations.game.shared;

import java.io.Serializable;

/**
 * @author CaseyF
 *
 */
public class Question implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String difficulty;
	private String question;
	private String answer1;
	private String answer2;
	private String answer3;
	private String answer4;
	private String category;
	
	/**
	 * @return the difficulty
	 */
	public String getDifficulty() {
		return difficulty;
	}
	
	/**
	 * @param difficulty the difficulty to set
	 */
	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}
	
	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}
	
	/**
	 * @param question the question to set
	 */
	public void setQuestion(String question) {
		this.question = question;
	}
	
	/**
	 * @return the answer1
	 */
	public String getAnswer1() {
		return answer1;
	}
	
	/**
	 * @param answer1 the answer1 to set
	 */
	public void setAnswer1(String answer1) {
		this.answer1 = answer1;
	}
	
	/**
	 * @return the answer2
	 */
	public String getAnswer2() {
		return answer2;
	}
	
	/**
	 * @param answer2 the answer2 to set
	 */
	public void setAnswer2(String answer2) {
		this.answer2 = answer2;
	}
	
	/**
	 * @return the answer3
	 */
	public String getAnswer3() {
		return answer3;
	}
	
	/**
	 * @param answer3 the answer3 to set
	 */
	public void setAnswer3(String answer3) {
		this.answer3 = answer3;
	}
	
	/**
	 * @return the answer4
	 */
	public String getAnswer4() {
		return answer4;
	}
	
	/**
	 * @param answer4 the answer4 to set
	 */
	public void setAnswer4(String answer4) {
		this.answer4 = answer4;
	}
	
	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	
	/**
	 * Generic constructor for a question, sets all members to empty strings. 
	 */
	public Question() {
		this("","","","","","","");
	}
	
	
	/**
	 * Create a question object, setting all fields available.
	 * @param category  a String representing the category of question
	 * @param question a String containing the question associated with this
	 * @param answer1 a String containing the correct answer
	 * @param answer2 a String containing an incorrect answer
	 * @param answer3 a String containing an incorrect answer
	 * @param answer4 a String containing an incorrect answer
	 * @param difficulty a String representing the difficulty of the question
	 */
	public Question(String category, String question, String answer1, String answer2, String answer3,
			String answer4, String difficulty){
		this.category = category;
		this.question = question;
		this.answer1 = answer1;
		this.answer2 = answer2;
		this.answer3 = answer3;
		this.answer4 = answer4;
		this.difficulty = difficulty;
	}


}
