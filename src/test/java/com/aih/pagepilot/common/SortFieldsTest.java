package com.aih.pagepilot.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SortFieldsTest {

    @Test
    void resolvesAllowListedFields() {
        assertEquals("priority", SortFields.resolve("priority", SortFields.APP));
        assertEquals("userAccount", SortFields.resolve("userAccount", SortFields.USER));
        assertEquals("createTime", SortFields.resolve("createTime", SortFields.CHAT_HISTORY));
        assertEquals("id", SortFields.resolve(" id ", SortFields.APP));
    }

    @Test
    void unknownOrBlankReturnsNull() {
        assertNull(SortFields.resolve(null, SortFields.APP));
        assertNull(SortFields.resolve("", SortFields.APP));
        assertNull(SortFields.resolve("   ", SortFields.USER));
        assertNull(SortFields.resolve("id;select", SortFields.APP));
        assertNull(SortFields.resolve("priority desc", SortFields.APP));
        assertNull(SortFields.resolve("userAccount", SortFields.APP));
        assertNull(SortFields.resolve("message", SortFields.CHAT_HISTORY));
    }
}
