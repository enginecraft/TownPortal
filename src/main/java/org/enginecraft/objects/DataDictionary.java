package org.enginecraft.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DataDictionary {
    public final String ref;
    public List<String[]> data;
    public String error;
}
