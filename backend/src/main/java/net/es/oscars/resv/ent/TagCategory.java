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
@AllArgsConstructor(suppressConstructorProperties=true)
@NoArgsConstructor
public class TagCategory {
    @JsonCreator
    public TagCategory(@JsonProperty("category") @NonNull String category) {
        this.category = category;
    }

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String category;


}
