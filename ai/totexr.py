#!/usr/bin/python

import feedparser
import time
from subprocess import check_output
import sys
import csv

All_RSS=['http://chicagotribune.feedsportal.com/c/34253/f/622872/index.rss',
         'http://feeds.reuters.com/reuters/companyNews',
         'http://feeds.feedburner.com/NdtvNews-TopStories',
         'http://feeds.feedburner.com/ndtv/TqgX',
         'http://feeds.feedburner.com/NDTV-Trending',
         'http://www.foxnews.com/about/rss/',
         'http://rss.cnn.com/rss/cnn_topstories.rss',
         'http://rss.cnn.com/rss/cnn_us.rss',
         'http://rss.cnn.com/rss/money_latest.rss',
         'http://feeds.reuters.com/reuters/businessNews',
         'http://www.forbes.com/markets/feed/',
         'http://www.fandango.com/rss/top10boxoffice.rss',
         'http://feeds.feedburner.com/thr/news',
         'https://www.oracle.com/corporate/press/rss/rss-pr.xml',
         'http://ir.teslamotors.com/releases.cfm',
         'http://articlefeeds.nasdaq.com/nasdaq/categories?category=Business',
         'http://www.espncricinfo.com/rss/content/story/feeds/0.xml',
         'http://www.cnet.com/rss/news/',
         'http://feeds.feedburner.com/Mashable',
         'http://sports.espn.go.com/espn/rss/news',
         'http://rss.imdb.com/daily/born/',
         'http://www.nfl.com/rss/rsslanding?searchString=home',
         'http://www.podquiz.com/podquiz.rss']

writter = csv.writer(open('cleanCap.csv', 'wb', buffering=0))
counter =1
#writter.writerows([
#            ('Headline', 'Answer', 'TimeStamp', 'Source'),
#           ])

# function to get the current time
current_time_millis = lambda: int(round(time.time() * 1000))
current_timestamp = current_time_millis()

#function to eliminate duplicates
def post_is_in_file(title):
    currentfeeds = csv.reader(open('cleanCap.csv', 'rb'))
    for currenttitle in currentfeeds:
        if title in currenttitle:
           return True
    return False

# return true if the title is in the database with a timestamp > limit
def post_is_in_file_with_old_timestamp(title):
    currentfeeds = csv.reader(open('cleanCap.csv', 'rb'))
    for currenttitle,answer,time,link in currentfeeds:
            if time in currenttitle:
                ts = long(time)
                if current_timestamp - ts > limit:
                    return True
    return False

#function to breakinto question
def break_into_question(title):
    #Eliminate classic RSS keywords
    title = title.replace('CORRECTION','')
    title = title.replace('UPDATE','')
    #Look for camelcase to idetify a nown
    #Dont want the first word to be missing
    splitquestion = title.split(' ',1);
    remainingqustion = str(splitquestion[1]).split()
    for eachword in remainingqustion:
        if(eachword != eachword.lower() and eachword != eachword.upper()
           and "'" not in eachword and "," not in eachword
           and ":" not in eachword and "?" not in eachword
           and "!" not in eachword and "#" not in eachword
           and "-" not in eachword and "_" not in eachword
           and "." not in eachword and ";" not in eachword
           and "Rs" not in eachword and "$" not in eachword
           and len(eachword)>3 and len(eachword)<7):
            answer = eachword
            fillblank = ''.ljust(len(answer),'*')
            title = title.replace(answer,fillblank)
            writter.writerows([
                (title, answer, str(current_timestamp), url),
                ])
            break;
        

def go_for_data(url):
    # get the feed data from the url
    feed = feedparser.parse(url)
    # figure out which posts to print
    #
    posts_to_print = []
    posts_to_skip = []

    for post in feed.entries:
        # if post is already in the database, skip it
        title = post.title
        if post_is_in_file_with_old_timestamp(title):
            posts_to_skip.append(title)
        else:
            posts_to_print.append(title)
    
    for title in posts_to_print:
        break_into_question(title)

for url in All_RSS:
    print('Getting feed from ->  ' + url)
    go_for_data(url)
