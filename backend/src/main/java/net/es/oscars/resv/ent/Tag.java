package net.es.oscars.resv.ent;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @JsonCreator
    public Tag(@JsonProperty("category") @NonNull String category,
               @JsonProperty("contents") @NonNull String contents) {
        this.category = category;
        this.contents = contents;
    }

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String category;

    @NonNull
    private String contents;

}
