package edu.stanford.cse193a.madlibs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Story implements Serializable {

    private String fileText;
    private List<Placeholder> placeholders;
    private boolean isBold;

    Story(Scanner scan, boolean isBold) {
        placeholders = new ArrayList<>();
        this.isBold = isBold;

        StringBuilder builder = new StringBuilder();

        while (scan.hasNextLine()) {
            builder.append(scan.nextLine()).append("\n");
        }
        fileText = builder.toString();

        int leftIndex = fileText.indexOf("<");
        int rightIndex = fileText.indexOf(">");
        while (leftIndex < rightIndex) {
            String word = fileText.substring(leftIndex + 1, rightIndex);
            placeholders.add(new Placeholder(leftIndex, word));

            leftIndex = fileText.indexOf("<", rightIndex + 1);
            rightIndex = fileText.indexOf(">", rightIndex + 1);
        }
    }

    int getNumberOfPlaceHolders() {
        return placeholders.size();
    }

    String getPlaceholder(int index) {
        Placeholder placeholder = placeholders.get(index);
        String word = placeholder.originalText;
        return ("aeiou".contains(word.substring(0, 1).toLowerCase())? "an ": "a ")
                + word.toLowerCase();
    }

    void setPlaceholder(int index, String userWord) {
        placeholders.get(index).userText = userWord;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(fileText);

        for (int i = placeholders.size() - 1; i >= 0; i--) {
            Placeholder placeholder = placeholders.get(i);

            int size = placeholder.originalText.length() + 2;
            int pos = placeholder.positionInFile;

            String res = placeholder.userText;
            if (isBold) {
                res = "<b>" + placeholder.userText + "</b>";
            }
            builder.replace(pos, pos + size, res);
        }

        return builder.toString();
    }

    class Placeholder {
        String originalText;
        String userText;
        int positionInFile;

        Placeholder(int positionInFile, String originalText) {
            this.positionInFile = positionInFile;
            this.originalText = originalText;
            userText = "";
        }
    }
}