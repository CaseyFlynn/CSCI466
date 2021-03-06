package com.flynnovations.game.client;

import java.util.*;

import com.flynnovations.game.shared.Question;

import java.io.*;
import java.nio.file.*;

public class FlatFileData implements IData, Serializable {

	private static final long serialVersionUID = 1L;
	private ArrayList<Question> allQuestions;
	private final String FILE_NAME = "XL_Example.csv";
	private Random random;
	
	
	/**
	 * Create a FlatFileData object that will return questions from the provided
	 * flat file.
	 */
	public FlatFileData() {
		allQuestions = new ArrayList<>();
		readQuestionFile();
		random = new Random(System.nanoTime());
	}
	
	/**
	 * Returns a random question from the flat file
	 */
	@Override
	public Question getQuestion() {
		// get a random question
		return allQuestions.get(random.nextInt(allQuestions.size()));
	}
	
	/**
	 * Read the question file into allQuestions
	 */
	private void readQuestionFile() {
		try {
			//TODO: move to configuration class
			Path currentRelativePath = Paths.get("");
			String filePath = currentRelativePath.toAbsolutePath().toString() + "/" + FILE_NAME;
			
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			//read and discard header line
			String curLine = reader.readLine();
			StringBuilder sb = new StringBuilder();
	
			//create question fields
			String[] qf = new String[7];
			int curField = 0;

			while ((curLine = reader.readLine()) != null) {
				//parse the line
				boolean quoted = false;
				for (char c : curLine.toCharArray()) {
					if (c == '"') {
						quoted = !quoted;
						continue;
					} else if (c == ',' && !quoted) {
						qf[curField] = sb.toString().trim();
						curField++;
						//clear string builder
						sb.setLength(0);
					} else {
						sb.append(c);
					}
				}
				//append the last string
				qf[curField] = sb.toString().trim();
				
				//create question
				Question q = new Question(qf[0],qf[1],qf[2],qf[3],qf[4],qf[5],qf[6]);
				
				allQuestions.add(q);
				
				//clear the string builder, reset field index
				sb.setLength(0);
				curField = 0;
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
