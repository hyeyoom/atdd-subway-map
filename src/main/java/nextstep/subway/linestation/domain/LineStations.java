package nextstep.subway.linestation.domain;

import com.google.common.collect.Lists;
import nextstep.subway.station.domain.Station;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Embeddable
public class LineStations {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "line_id", foreignKey = @ForeignKey(name = "fk_line_station_to_line"))
    private List<LineStation> lineStations = Lists.newArrayList();

    public List<LineStation> getLineStations() {
        return lineStations;
    }

    public List<LineStation> getStationsInOrder() {
        Optional<LineStation> preLineStation = lineStations.stream()
                .filter(it -> it.getFormerStation() == null)
                .findFirst();

        final List<LineStation> result = new ArrayList<>();
        while (preLineStation.isPresent()) {
            final LineStation formerStation = preLineStation.get();
            result.add(formerStation);
            preLineStation = lineStations.stream()
                    .filter(it -> it.getFormerStation() == formerStation.getStation())
                    .findFirst();
        }
        return result;
    }

    public void add(LineStation lineStation) {
        checkValidation(lineStation);

        lineStations.stream()
                .filter(it -> it.getFormerStation() == lineStation.getFormerStation())
                .findFirst()
                .ifPresent(it -> it.updatePreStationTo(lineStation.getStation()));

        lineStations.add(lineStation);
    }

    private void checkValidation(LineStation lineStation) {
        if (lineStation.getStation() == null) {
            throw new RuntimeException("스테이션 정보가 비어있어요!");
        }

        if (lineStations.stream().anyMatch(it -> it.isSame(lineStation))) {
            throw new RuntimeException();
        }
    }

    public void removeByStationId(Station station) {
        LineStation lineStation = lineStations.stream()
                .filter(it -> it.getStation().equals(station))
                .findFirst()
                .orElseThrow(RuntimeException::new);

        lineStations.stream()
                .filter(it -> it.getFormerStation().equals(station))
                .findFirst()
                .ifPresent(it -> it.updatePreStationTo(lineStation.getFormerStation()));

        lineStations.remove(lineStation);
    }
}
