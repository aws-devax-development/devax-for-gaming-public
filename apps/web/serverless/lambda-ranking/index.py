import requests
import os
import random
from aws_xray_sdk.core import patcher, xray_recorder

patcher.patch(('requests',))
xray_recorder.configure(service='details-caller')

def handler(event, context):
    if 'pathParameters' not in event or 'ProductID' not in event['pathParameters']:
        raise Exception('pathParameters.ProductID does not exists')
    productId = event['pathParameters']['ProductID']
    version = get_version()
    if hasattr(event, 'headers') and hasattr(event.headers, 'version'):
        version = event.headers['version']

    response = requests.get('http://' + os.getenv('DETAILS_MAIN_URL') + '/details/' +productId, headers={'version': version})
    headers = {}
    headers['Content-Type'] = 'Application/json'
    add_access_control_headers(headers)
    return compose_response(200, headers, response.text)

def compose_response(status, headers, body):
    result = {}
    result['statusCode'] = status
    result['headers'] = headers
    result['body'] = body
    return result

def add_access_control_headers(headers):
    headers['access-control-allow-headers'] = '*'
    headers['access-control-allow-methods'] = '*'
    headers['access-control-allow-origin'] = '*'

def get_version():
    num = random.randrange(10)
    if num >= 4:
        return "new"
    return "prod"