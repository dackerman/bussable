package com.dacklabs.bustracker.application.requests;

import com.google.common.collect.Sets;

import java.util.Set;

public interface Message {
    Set<Message> NONE = Sets.newHashSet();
}
