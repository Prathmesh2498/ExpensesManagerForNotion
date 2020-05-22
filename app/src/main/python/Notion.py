# -*- coding: utf-8 -*-
"""
Created on Sat May  3 15:22:31 2020

@author: Prathmesh D
"""

def getTags(v2, db):
    from notion.client import NotionClient

    try:
        client = NotionClient(token_v2=v2)
        print("Connected")

    except:
        print("Error v2")

    try:
        cv = client.get_collection_view(db)
        print("Connected")
    except:
        print("Error db")

    try:
        values=[]
        a = cv.collection.get_schema_properties()
        s = a[2]['id']
        b=cv.collection.get_schema_property(s)
        for c in b['options']:
            values.append(c['value'])
            print("Added {}".format(c['value']))
        return values
    except:
        return values





def checkConnect(v2,db):
    from notion.client import NotionClient

    s="Success"
    try:
            client = NotionClient(token_v2=v2)

    except:
        s ="V2Error"
        return s

    try:
        cv = client.get_collection_view(db)

    except:
        s="dbError"
        return s

    return s

def addRecord(Expense,Amount,Comment,Category,v2,db):
    import datetime
    from notion.client import NotionClient
    Date = datetime.datetime.now()

    print(v2)
    print(db)
    # Obtain the `token_v2` value by inspecting your browser cookies on a logged-in session on Notion.so

    try:
        client = NotionClient(token_v2=v2)
        print("Client Connected..")
    except:
        s ="V2Error"
        return s

    try:
        cv = client.get_collection_view(db)
        print("Connected to db")
    except:
        s="dbError"
        return s

    try:
        row = cv.collection.add_row()

        row.Expense = Expense
        row.Amount = Amount
        row.Comment = Comment
        row.Category = Category
        row.Date=Date
        print("Record Added!")
        return "Success"
    except Exception as e:
        s = str(e)
        filter_params = [{
            "property": "Expense",
        }]
        result = cv.build_query(filter=filter_params).execute()
        print(result)
        size = len(result)
        result[size-1].remove()
        return s