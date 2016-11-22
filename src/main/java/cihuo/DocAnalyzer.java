package cihuo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

public class DocAnalyzer {

	private static final String CONFIGFILE = "/config.properties";

	// private static final String RESULTFILE = "./result.txt";

	public static void main(String[] args) {
		long start = System.currentTimeMillis();


		// initialize
		Properties prop = new Properties();
		try {
			if (args != null && args.length > 0) {
				prop.load(DocAnalyzer.class.getResourceAsStream(args[0]));
			}else{
				prop.load(DocAnalyzer.class.getResourceAsStream(CONFIGFILE));
			}

			String SOURCEFOLDER = prop.getProperty("SOURCEFOLDER");
			String SPECIALWORDS = prop.getProperty("SPECIALWORDS");
			int MINCOUNT = Integer.parseInt(prop.getProperty("MINCOUNT"));
			int COUNT_OF_WORDS = Integer.parseInt(prop.getProperty("COUNT_OF_WORDS"));

			// filter keys containing numbers
			Predicate<String> isAlpha = s -> s.matches("[a-z]+") && s.length() > 2;
			Predicate<String> allAlpha = s -> Arrays.stream(s.split(" ")).allMatch(isAlpha);
			// filter keys containing special words
			Predicate<String> isSpecialWord = s -> s.matches("^(" + SPECIALWORDS + ")$");
			Predicate<String> withSpecialWords = s -> Arrays.stream(s.split(" ")).noneMatch(isSpecialWord);

			File file = new File(SOURCEFOLDER);
			if (file.exists()) {
				Map<String, Long> resultMap = new HashMap<>();
				for (File doc : file.listFiles()) {
					String wholeDoc = readDocAsString(doc);
					switch(COUNT_OF_WORDS){
						case 1:
							analyze1WordCount(wholeDoc, resultMap);
							break;
						case 2:
							analyze2WordCount(wholeDoc, resultMap);
							break;
						case 3:
							analyze3WordCount(wholeDoc,resultMap);
							break;
						case 4:
							analyze4WordCount(wholeDoc,resultMap);
							break;
						default:
							System.out.println("Suport only 1/2/3/4 word/words analysis.");
							return;
					}
				}
				// output
				resultMap
						.entrySet()
						.stream()
						.filter(x -> allAlpha.test(x.getKey()))
						.filter(x -> withSpecialWords.test(x.getKey()))
						.filter(x -> x.getValue() >= MINCOUNT)
						.sorted(Map.Entry.comparingByValue())
						.forEach(
								x -> System.out.println(x.getKey() + "="
										+ x.getValue()));

				long end = System.currentTimeMillis();
				System.out.println("Time cost: " + (end - start) / 1000
						+ " seconds");

			}else{
				System.out.println("Cannot find the source folder!");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void analyze1WordCount(String doc, Map<String, Long> map){
		String[] words = doc.replaceAll("^[A-Za-z-]", " ").toLowerCase()
				.replaceAll("\\s{2,}", " ").trim().split(" ");
		for (int i = 0; i < words.length; i++) {
			if (map.containsKey(words[i])) {
				map.put(words[i], map.get(words[i]) + 1);
			} else {
				map.put(words[i], Long.valueOf(1));
			}
		}
	}

	private static void analyze2WordCount(String doc, Map<String, Long> map) {
		String[] words = doc.replaceAll("^[A-Za-z]", " ").toLowerCase()
				.replaceAll("\\s{2,}", " ").trim().split(" ");
		String last = words[0];
		String key = null;
		for (int i = 1; i < words.length; i++) {
			key = last + " " + words[i];
			if (map.containsKey(key)) {
				map.put(key, map.get(key) + 1);
			} else {
				map.put(key, Long.valueOf(1));
			}
			last = words[i];
		}
	}

	private static void analyze3WordCount(String doc, Map<String, Long> map) {
		String[] words = doc.replaceAll("^[A-Za-z]", " ").toLowerCase()
				.replaceAll("\\s{2,}", " ").trim().split(" ");
		String first = words[0];
		String second = words[1];
		String key = null;
		for (int i = 2; i < words.length; i++) {
			key = first + " " + second + " " + words[i];
			if (map.containsKey(key)) {
				map.put(key, map.get(key) + 1);
			} else {
				map.put(key, Long.valueOf(1));
			}
			first = second;
			second = words[i];
		}
	}

	private static void analyze4WordCount(String doc, Map<String, Long> map) {
		String[] words = doc.replaceAll("^[A-Za-z]", " ").toLowerCase()
				.replaceAll("\\s{2,}", " ").trim().split(" ");
		String first = words[0];
		String second = words[1];
		String third = words[2];
		String key = null;
		for (int i = 3; i < words.length; i++) {
			key = first + " " + second + " " + third + " " + words[i];
			if (map.containsKey(key)) {
				map.put(key, map.get(key) + 1);
			} else {
				map.put(key, Long.valueOf(1));
			}
			first = second;
			second = third;
			third = words[i];
		}
	}

	private static String readDocAsString(File doc) {
		StringBuilder sb = new StringBuilder();
		try {
			PdfReader reader = new PdfReader(doc.getAbsolutePath());
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			TextExtractionStrategy strategy;
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				strategy = parser.processContent(i,
						new SimpleTextExtractionStrategy());
				sb.append(strategy.getResultantText());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString().replaceAll("\n", "").replaceAll("\\.", "\n");
	}

}
