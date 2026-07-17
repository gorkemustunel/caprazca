package com.example

import com.example.data.model.WordEntity
import com.example.data.repository.DictionaryData
import com.example.data.repository.CrosswordLayoutEngine
import org.junit.Test
import java.util.Locale

class DebugLevel2Test {

    @Test
    fun debugLevel2Generation() {
        val dictionary = DictionaryData.getPreloadedWords().associateBy { it.id.lowercase(Locale.ENGLISH) }
        val wordIds = listOf("please", "sorry", "no")
        val result = CrosswordLayoutEngine.generateLayoutResult(2, wordIds, dictionary)
        println("Generation Result: $result")
    }
}
