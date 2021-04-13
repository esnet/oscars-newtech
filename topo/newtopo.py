#!/usr/bin/env python3
# encoding: utf-8
import requests
import pprint

pp = pprint.PrettyPrinter(indent=2)


def main():
    settings = {
        'VERBOSE': False,
        'URLS': {
            'ESDB': "https://esdb.es.net/esdb_api/v1",
        },
        'DATASETS': {

            'EQUIPMENT': {
                'SOURCE': 'ESDB',
                'URL': '/equipment/?limit=0&detail=list',
                'CACHE_FILENAME': 'equipment.json'
            },
            'EQUIPMENT_INTERFACE': {
                'SOURCE': 'ESDB',
                'URL': '/equipment_interface/?limit=0&detail=list',
                'CACHE_FILENAME': 'eq_ifce.json'
            },

            'LOCATIONS': {
                'SOURCE': 'ESDB',
                'URL': '/location/?limit=0',
                'CACHE_FILENAME': 'locations.json'
            }

        },
    }
    print("hi")


if __name__ == '__main__':
    main()
