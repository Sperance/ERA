package com.example.helpers

import com.example.logging.DailyLogger.printTextLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import io.ktor.util.Attributes
import kotlinx.serialization.Serializable

object LocationInfo {

    val attrGEO = AttributeKey<IpApiResponse>("GEO")

    @Serializable
    data class IpApiResponse (
        var status      : String? = null,
        var country     : String? = null,
        var countryCode : String? = null,
        var region      : String? = null,
        var regionName  : String? = null,
        var city        : String? = null,
        var zip         : String? = null,
        var lat         : Double? = null,
        var lon         : Double? = null,
        var timezone    : String? = null,
        var isp         : String? = null,
        var org         : String? = null,
        var `as`        : String? = null,
        var query       : String? = null
    ) {
        override fun toString(): String {
            return "IpApiResponse(country=$country, countryCode=$countryCode, region=$region, regionName=$regionName, city=$city, zip=$zip, lat=$lat, lon=$lon, timezone=$timezone, isp=$isp, org=$org, `as`=$`as`, query=$query)"
        }

        fun toFormatString(): String {
            return "{$query} [$countryCode]($country) $regionName $city 'lat=$lat lon=$lon'"
        }

        fun toFormatAttributes(): Attributes {
            val attributes = Attributes(true)
            attributes.put(attrGEO, this)
            return attributes
        }
    }

    private val clientInfo = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun getLocationByIp(ip: String): IpApiResponse {
        printTextLog("[getLocationByIp] ip: $ip")
        val response: IpApiResponse = clientInfo
            .get("http://ip-api.com/json/$ip")
            .body()

        printTextLog("[getLocationByIp] response: ${response.toFormatString()}")

        return response
    }
}