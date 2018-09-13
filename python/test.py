import csv
import requests
import threading
import datetime
import time

bingApiKey = "AqS4Ne5sT_qCkBBMTzu9PrT0nTzTkI0XDl8Npw6AEr1DsvV0UheG0XT4j20CNXuc"
trafficApiURL = "http://dev.virtualearth.net/REST/V1/Routes/Driving?"


googleApiKey = "AIzaSyC40g_wv8AvJe2gJ4mReUELdx4TAN1cbhM"
directionApiURL = "https://maps.googleapis.com/maps/api/directions/json?"


# with open('test.csv', 'w') as csvfile:
#     fieldnames = ['date_var', 'route', 'congestion score']
#     writer = csv.DictWriter(csvfile, fieldnames=fieldnames, lineterminator='\n')
#     writer.writeheader()

while True:
    print "loop restart"
    googleResponse = requests.get(directionApiURL + "&origin=Grange+Road,+Mount+Eden,+Auckland,"
                                                    "&destination=Symonds+Street,+Eden+Terrace,+Auckland"
                                                    "&alternatives=true&mode=bicycling&key=" + googleApiKey)

    googleData = googleResponse.json()
    jsonRoutes = googleData["routes"]

    congestionScoreArray = [0.0,0.0,0.0]
    for i, Route in enumerate(jsonRoutes):
        congestionScore = 0.0
        jsonLegs = Route["legs"]
        jsonLeg = jsonLegs[0]
        jsonSteps = jsonLeg["steps"]

        for j in jsonSteps:

            startLoc = j["start_location"]
            endLoc = j["end_location"]

            lat1 = startLoc["lat"]
            lon1 = startLoc["lng"]

            lat2 = endLoc["lat"]
            lon2 = endLoc["lng"]

            bingResponse = requests.get(trafficApiURL + "wp.1=" + str(lat1) + "," + str(lon1) + "&wp.2="
                                        + str(lat2) + "," + str(lon2) + "&key=" + bingApiKey)

            bingData = bingResponse.json()
            resourceSets = bingData["resourceSets"]
            jsonObject = resourceSets[0]
            resources = jsonObject["resources"]
            jsonObject = resources[0]

            congestionString = jsonObject["trafficCongestion"]

            if congestionString == "Unknown":
                congestionScore += 0
            elif congestionString == "Light":
                congestionScore += 1
            elif congestionString == "Mild":
                congestionScore += 2
            elif congestionString == "Medium":
                congestionScore += 3
            elif congestionString == "Heavy":
                congestionScore += 4
            elif congestionString == "None":
                congestionScore += 0
            else:
                print("ERROR NO CONGESTION SCORE RECORDED")

        congestionScoreArray[i] = congestionScore / len(jsonSteps)
        congestionScoreArray[i] = round(congestionScoreArray[i],2)

    with open('test.csv', 'a') as csvfile:
        fieldnames = ['date_var', 'route', 'congestion score']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames, lineterminator='\n')
        for i, Value in enumerate(congestionScoreArray):
            writer.writerow({'date_var': datetime.datetime.now(),
                             'route': i,
                             'congestion score': Value})
    time.sleep(180)
