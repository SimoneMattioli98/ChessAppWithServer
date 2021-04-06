import requests

BASE = "http://1b87cdc7a22e.ngrok.io/"



response = requests.get(BASE + "images")
print(response.json())

