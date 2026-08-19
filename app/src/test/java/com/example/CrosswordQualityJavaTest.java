package com.example;

import com.example.data.model.CrosswordDirection;
import com.example.data.model.CrosswordWord;
import com.example.data.model.PuzzleLevel;
import com.example.data.model.WordEntity;
import com.example.data.repository.CrosswordGenerationResult;
import com.example.data.repository.CrosswordLayoutEngine;
import com.example.data.repository.DictionaryData;
import com.example.data.repository.LevelsData;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class CrosswordQualityJavaTest {

    @Test
    public void playableLevelsGenerateConnectedCompactNonSpoilerLayouts() {
        List<PuzzleLevel> levels = LevelsData.INSTANCE.getLevels();
        Map<String, WordEntity> dictionary = new HashMap<>();
        for (WordEntity word : DictionaryData.INSTANCE.getPreloadedWords()) {
            dictionary.put(word.getId().toLowerCase(Locale.ENGLISH), word);
        }

        List<String> errors = new ArrayList<>();
        for (PuzzleLevel level : levels) {
            if (level.isChest()) continue;

            CrosswordGenerationResult result = CrosswordLayoutEngine.INSTANCE.generateLayoutResult(
                    level.getId(),
                    level.getWordIds(),
                    dictionary
            );

            if (result instanceof CrosswordGenerationResult.Failure) {
                CrosswordGenerationResult.Failure failure = (CrosswordGenerationResult.Failure) result;
                errors.add("Level " + level.getId() + ": " + failure.getReason() + "; unplaced=" + failure.getUnplacedWordIds());
                continue;
            }

            CrosswordGenerationResult.Success success = (CrosswordGenerationResult.Success) result;
            int maxDimension = level.getId() <= 20 ? 10 : level.getId() <= 60 ? 11 : 12;
            if (success.getRows() > maxDimension || success.getColumns() > maxDimension) {
                errors.add("Level " + level.getId() + ": layout too large " + success.getRows() + "x" + success.getColumns());
            }
            if (!isConnected(success.getWords())) {
                errors.add("Level " + level.getId() + ": layout is disconnected");
            }
            for (CrosswordWord placedWord : success.getWords()) {
                WordEntity dictionaryWord = dictionary.get(placedWord.getWordId());
                if (dictionaryWord != null && clueRevealsAnswer(placedWord, dictionaryWord)) {
                    errors.add("Level " + level.getId() + ": clue reveals " + placedWord.getWordId());
                }
            }
        }

        assertTrue("Crossword quality errors: " + errors, errors.isEmpty());
    }

    private static boolean clueRevealsAnswer(CrosswordWord word, WordEntity dictionaryWord) {
        String clue = normalize(word.getClueTurkish());
        for (String part : dictionaryWord.getTurkish().split("[/,;]")) {
            String answer = normalize(part);
            if (answer.length() >= 3 && clue.contains(answer)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isConnected(List<CrosswordWord> words) {
        if (words.size() <= 1) return true;
        for (CrosswordWord word : words) {
            boolean intersects = false;
            Set<String> wordCells = cells(word);
            for (CrosswordWord other : words) {
                if (word == other) continue;
                Set<String> otherCells = cells(other);
                for (String cell : wordCells) {
                    if (otherCells.contains(cell)) {
                        intersects = true;
                        break;
                    }
                }
                if (intersects) break;
            }
            if (!intersects) return false;
        }
        return true;
    }

    private static Set<String> cells(CrosswordWord word) {
        Set<String> cells = new HashSet<>();
        for (int index = 0; index < word.getAnswerEnglish().length(); index++) {
            int row = word.getDirection() == CrosswordDirection.HORIZONTAL ? word.getRow() : word.getRow() + index;
            int col = word.getDirection() == CrosswordDirection.HORIZONTAL ? word.getCol() + index : word.getCol();
            cells.add(row + ":" + col);
        }
        return cells;
    }

    private static String normalize(String value) {
        return value
                .toLowerCase(new Locale("tr", "TR"))
                .replaceAll("[^a-zçğıöşü0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
