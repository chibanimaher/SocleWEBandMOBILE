#!/usr/bin/python

import sys, json


def main():
    task = sys.argv[1]
    nameFile = ''

    if task == 'getFileAPP':
        loaded_json = json.loads(sys.stdin.read())
        countFile =  len(loaded_json["data"])
        #print countFile
        for x in range(countFile):
            if loaded_json["data"][x]["inputType"]=="APPLICATION":
                nameFile = loaded_json["data"][x]["name"]
        print nameFile
    elif task == 'getFileAPPID':
        loaded_json = json.loads(sys.stdin.read())
        countFile =  len(loaded_json["data"])
        #print countFile
        for x in range(countFile):
            if loaded_json["data"][x]["inputType"]=="APPLICATION":
                nameFile = loaded_json["data"][x]["id"]
        print nameFile
    elif task == 'getFileTEST':
        loaded_json = json.loads(sys.stdin.read())
        countFile =  len(loaded_json["data"])
        #print countFile
        for x in range(countFile):
            if loaded_json["data"][x]["inputType"]=="TEST":
                nameFile = loaded_json["data"][x]["name"]
        print nameFile
    elif task == 'getFileTESTID':
        loaded_json = json.loads(sys.stdin.read())
        countFile =  len(loaded_json["data"])
        #print countFile
        for x in range(countFile):
            if loaded_json["data"][x]["inputType"]=="TEST":
                nameFile = loaded_json["data"][x]["id"]
        print nameFile

if __name__ == "__main__":
   main()