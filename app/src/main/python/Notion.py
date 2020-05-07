# -*- coding: utf-8 -*-
"""
Created on Sat May  3 15:22:31 2020

@author: Prathmesh D
"""


def addRecord(Expense,Amount,Comment,Category):
    import datetime
    from notion.client import NotionClient
    Date = datetime.datetime.now()

    # Obtain the `token_v2` value by inspecting your browser cookies on a logged-in session on Notion.so
    client = NotionClient(token_v2="YOUR_URL_HERE")
    print("Client Connected..")
    cv = client.get_collection_view("YOUR_DATABASE_URL_HERE")
    print("Connected to db")


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






