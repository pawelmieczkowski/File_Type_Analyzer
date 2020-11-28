package analyzer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.System.nanoTime;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide input");
            System.exit(0);
        }
        String dirPath = args[0];
        String patternPath = args[1];

        List<String> patterns = new ArrayList<>();
        Scanner patternScanner = null;
        try {
            patternScanner = new Scanner(new File(patternPath));
            while (patternScanner.hasNext()) {
                patterns.add(patternScanner.nextLine());
            }
            patternScanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //potentially need to implement sorting
        Collections.reverse(patterns);

        ExecutorService executor = Executors.newCachedThreadPool();

        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                executor.submit(() -> {
                    KMPSearch(f, patterns);
                });
            }
        }
        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void KMPSearch(File fileToCheck, List<String> patterns) {
        try (
                InputStream inputStream = new FileInputStream(fileToCheck);
        ) {
            String text = new String(inputStream.readAllBytes());
            for (String s : patterns) {
                String pattern = s.split(";")[1].replace("\"", "");
                String resultString = s.split(";")[2].replace("\"", "");
                List<Integer> KMPresult = KMPSearch(text, pattern);
                if (KMPresult.size() > 0) {
                    System.out.println(fileToCheck + ": " + resultString);
                    return;
                }
            }
            System.out.println(fileToCheck + ": " + "Unknown file type");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static List<Integer> KMPSearch(String text, String pattern) {
        int[] prefixFunc = prefixFunction(pattern);
        ArrayList<Integer> occurrences = new ArrayList<Integer>();
        int j = 0;
        for (int i = 0; i < text.length(); i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = prefixFunc[j - 1];
            }
            if (text.charAt(i) == pattern.charAt(j)) {
                j += 1;
            }
            if (j == pattern.length()) {
                occurrences.add(i - j + 1);
                j = prefixFunc[j - 1];
            }
        }
        return occurrences;
    }

    public static int[] prefixFunction(String str) {
        int[] prefixFunc = new int[str.length()];
        for (int i = 1; i < str.length(); i++) {
            int j = prefixFunc[i - 1];
            while (j > 0 && str.charAt(i) != str.charAt(j)) {
                j = prefixFunc[j - 1];
            }
            if (str.charAt(i) == str.charAt(j)) {
                j += 1;
            }
            prefixFunc[i] = j;
        }
        return prefixFunc;
    }

}