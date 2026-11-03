package com.islamshikhi.app.feature.mosque

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MosqueFinderTest {

    @Test fun distance_sameBpointIsZero() {
        assertEquals(0, MosqueFinder.distanceMeters(23.78, 90.41, 23.78, 90.41))
    }

    @Test fun distance_oneDegreeLatitudeIsAbout111km() {
        val d = MosqueFinder.distanceMeters(23.0, 90.0, 24.0, 90.0)
        assertTrue(d in 110_000..112_500)
    }

    @Test fun parse_sortsByDistance_usesCenterForWays_andPrefersBanglaName() {
        val json = """
        {"elements":[
          {"type":"node","lat":23.7900,"lon":90.4100,"tags":{"name":"Far Mosque"}},
          {"type":"way","center":{"lat":23.7805,"lon":90.4105},"tags":{"name:bn":"কাছের মসজিদ","name":"Near","addr:street":"Road 1","addr:city":"Dhaka"}},
          {"type":"node","lat":23.7810,"lon":90.4110}
        ]}
        """.trimIndent()
        val r = MosqueFinder.parse(json, 23.7800, 90.4100)
        assertEquals(3, r.size)
        assertEquals("কাছের মসজিদ", r[0].name)
        assertEquals("Road 1, Dhaka", r[0].address)
        assertNull(r[1].name)
        assertEquals("Far Mosque", r[2].name)
        assertTrue(r[0].distanceM <= r[1].distanceM && r[1].distanceM <= r[2].distanceM)
    }

    @Test fun parse_emptyOrMissingElements_givesEmptyList() {
        assertTrue(MosqueFinder.parse("{}", 0.0, 0.0).isEmpty())
        assertTrue(MosqueFinder.parse("""{"elements":[]}""", 0.0, 0.0).isEmpty())
    }
}
