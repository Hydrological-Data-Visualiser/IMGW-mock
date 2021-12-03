package pl.edu.agh.imgwmock.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModflowModelInfo {

    private String name;

    private String lat;

    @JsonProperty("long")
    private String longitude;

    private String start_date;

    private String end_date;

    private Integer rows;

    private Integer cols;

    private String grid_unit;

    private List<Integer> row_cells;

    private List<Integer> col_cells;

    private String modflow_model;

    private List<String> hydrus_models;
}
