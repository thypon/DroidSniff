package com.evozi.droidsniff.model.event;

import com.evozi.droidsniff.model.auth.Auth;
import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public final class AuthEvent {
    @NonNull Auth auth;
    @NonNull AuthEventType type;
}
