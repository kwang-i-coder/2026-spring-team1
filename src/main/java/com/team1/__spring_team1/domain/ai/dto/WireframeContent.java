package com.team1.__spring_team1.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WireframeContent {

    private String type;
    private Integer width;
    private Integer height;
    private List<Element> elements;

    @Getter
    @NoArgsConstructor
    public static class Element {

        private String id;
        private String type;
        private String text;
        private Integer x;
        private Integer y;
        private Integer w;
        private Integer h;
    }
}