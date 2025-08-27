package com.survery.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.locationtech.jts.geom.Polygon;

import java.util.UUID;

@Data
@Entity
@Table(name = "constituencies")
public class Constituency {

    @Id
    @Column(name = "constituency_id")
    private UUID constituencyId;

    @Column(name = "constituency_name", nullable = false)
    private String constituencyName;

    @Column(name = "geo_boundary", nullable = false, columnDefinition = "geometry(Polygon,4326)")
    private Polygon geoBoundary;
}
