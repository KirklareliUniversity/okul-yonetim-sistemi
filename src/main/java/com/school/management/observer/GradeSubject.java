package com.school.management.observer;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class GradeSubject {
    private final List<GradeObserver> observers;

    public GradeSubject(List<GradeObserver> observers) {
        this.observers = observers;
    }

    public void notifyGradeAdded(GradeEvent event) {
        observers.forEach(observer -> observer.onGradeAdded(event));
    }
}
