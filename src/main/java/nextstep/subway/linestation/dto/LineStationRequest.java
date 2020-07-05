package nextstep.subway.linestation.dto;

public class LineStationRequest {
    private String preStationId;
    private String stationId;
    private String distance;
    private String duration;

    public LineStationRequest() {
    }

    public LineStationRequest(String preStationId, String stationId, String distance, String duration) {
        this.preStationId = preStationId;
        this.stationId = stationId;
        this.distance = distance;
        this.duration = duration;
    }

    public String getPreStationId() {
        return preStationId;
    }

    public String getStationId() {
        return stationId;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }
}
