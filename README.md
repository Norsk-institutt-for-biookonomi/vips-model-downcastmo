# DOWNCASTModel

<p>DOWNCAST was developed by de Visser (1998) in the Netherlands. It calculates infection risk of <em>Peronospora destructor</em> (Onion Downy Mildew) using weather data. An actual infection requires present inoculum. Criteria for sporulation and infection must be met for the model to issue a high infection risk (red alert).</p>
<p>Sporulation events are calculated based on high (over 92%) nightly relative humidity (RH). Sporulation may occur at temperatures between 4 and 26&nbsp;&deg;C (optimally 12-20 &deg;C). A sporulation index of 1-3 is calculated based on the high RH and temperature duration, where an index of 3 indicates optimal sporulation conditions. Sporulation events are not expected to occur if it's been raining between 00:00 and 06:00 AM, or the previous day was very warm, with several hours above 27 &deg;C.</p>
<p>Infection events are based on leaf wetness. A direct infection requires a minimum of 2 hours of leaf wetness the morning after sporulation (optimum temperature 6-16 &deg;C). Delayed&nbsp;infections may occur as long as 72 hours after sporulation, given at least 144 minutes of leaf wetness during a 3-hour period. It is assumed that all the spores germinate simultaneously, thus only one infection event can occur after a sporulation event.</p>
<p>Short periods of leaf wetness (90-150 minutes during a 5 hour period) may&nbsp;cause the spores to germinate and die. Under these circumstances we have a failed infection, and the potential infection event is cancelled. A later infection event requires a new sporulation event.</p>
<p>A sporulation event triggers a yellow alert&nbsp;(possible infection risk). A sporulation event followed by an infection event within 72 hours triggers a red alert (high infection risk).</p>
<h2>Literature</h2>
<p>de Visser, C.L.M. 1998. Development of a downy mildew advisory model based on downcast. European Journal of Plant Pathology 104: 933-943.</p>
<h2>Forecast interpretation (Norway)</h2>
<p>In Norway, Peronospora destructor normally occurs in the late summer, if at all. A spraying decision should therefore be based on an assessment of present inoculum. Spraying more than every 10th day is normally not necessary, but with frequent alerts and inoculum detected, a spraying interval close to 7 days may be considered.</p>
<p><img src="src/main/resources/images/lokbladskimmel.jpg" title="Peronospora destructor in spring onion. (Photo: Arne Hermansen, NIBIO)"/></p>
<p>&nbsp;</p>

### Development

Clone Gitlab repository

```
$ git clone https://gitlab.nibio.no/VIPS/models/java/Model_DOWNCASTMO.git
$ cd Model_DOWNCASTMO
```
