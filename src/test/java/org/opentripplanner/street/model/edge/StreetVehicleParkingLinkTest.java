package org.opentripplanner.street.model.edge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.opentripplanner.street.model._data.StreetModelForTest.intersectionVertex;
import static org.opentripplanner.transit.model._data.TransitModelForTest.id;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.opentripplanner.framework.geometry.WgsCoordinate;
import org.opentripplanner.framework.i18n.NonLocalizedString;
import org.opentripplanner.routing.api.request.StreetMode;
import org.opentripplanner.routing.api.request.request.VehicleParkingRequest;
import org.opentripplanner.routing.api.request.request.filter.VehicleParkingFilter;
import org.opentripplanner.routing.api.request.request.filter.VehicleParkingFilterRequest;
import org.opentripplanner.routing.vehicle_parking.VehicleParking;
import org.opentripplanner.routing.vehicle_parking.VehicleParkingEntrance;
import org.opentripplanner.street.model.vertex.VehicleParkingEntranceVertex;
import org.opentripplanner.street.model.vertex.Vertex;
import org.opentripplanner.street.search.request.StreetSearchRequest;
import org.opentripplanner.street.search.state.State;
import org.opentripplanner.test.support.VariableSource;

class StreetVehicleParkingLinkTest {

  static Stream<Arguments> testCases = Stream.of(
    of(Set.of(), Set.of(), Set.of(), true),
    of(Set.of("a-tag"), Set.of(), Set.of(), true),
    of(Set.of("a"), Set.of("a"), Set.of(), false),
    of(Set.of("a"), Set.of("a"), Set.of("a"), false),
    of(Set.of("a", "b"), Set.of("b"), Set.of("a"), false),
    of(Set.of("a", "b"), Set.of(), Set.of("a"), true),
    of(Set.of("a", "b"), Set.of(), Set.of("c"), false)
  );

  @ParameterizedTest(name = "Parking[tags={0}], Request[not={1}, select={2}] should traverse={3}")
  @VariableSource("testCases")
  void foo(Set<String> parkingTags, Set<String> not, Set<String> select, boolean shouldTraverse) {
    var streetVertex = intersectionVertex(1, 1);
    var parking = VehicleParking
      .builder()
      .id(id("parking"))
      .coordinate(new WgsCoordinate(1, 1))
      .tags(parkingTags)
      .build();

    var entrance = VehicleParkingEntrance
      .builder()
      .vehicleParking(parking)
      .entranceId(id("entrance"))
      .coordinate(new WgsCoordinate(1, 1))
      .name(new NonLocalizedString("entrance"))
      .walkAccessible(true)
      .carAccessible(true)
      .build();

    var entranceVertex = new VehicleParkingEntranceVertex(null, entrance);
    var parkingReq = new VehicleParkingRequest();

    Set<VehicleParkingFilter> notFilter = Set.of(new VehicleParkingFilter.TagsFilter(not));
    Set<VehicleParkingFilter> selectFilter = Set.of(new VehicleParkingFilter.TagsFilter(select));

    parkingReq.setFilter(new VehicleParkingFilterRequest(notFilter, selectFilter));

    var req = StreetSearchRequest.of();
    req.withMode(StreetMode.BIKE_TO_PARK);
    req.withParking(parkingReq);
    req.withPreferences(p -> p.withBike(bike -> bike.withParkCost(0)));

    var edge = new StreetVehicleParkingLink(streetVertex, entranceVertex);

    var result = traverse(streetVertex, edge, req.build());
    if (shouldTraverse) {
      assertFalse(State.isEmpty(result));
    } else {
      assertTrue(State.isEmpty(result));
    }
  }

  private State[] traverse(Vertex fromV, Edge edge, StreetSearchRequest request) {
    var state = new State(fromV, request);

    assertEquals(0, state.weight);
    return edge.traverse(state);
  }
}
