import csv
from num2words import num2words
import random
import time

counter = 000;
# write  data as comma-separated values
writter = csv.writer(open('numbers.csv', 'wb', buffering=0))
writter.writerows([
            ('quiestionId','question', 'answer', 'timestamp', 'credits'),
           ])

# function to get the current time
current_time_millis = lambda: int(round(time.time() * 1000))
current_timestamp = current_time_millis()

#function to get two operands
def getRandoms(i):
    if i%2 == 0:
        OP1 = random.randrange(1,100)
        OP2= random.randrange(0,100)
        makeQuestion('Fill in the missing word: The SUM of ' + str(OP1) + ' and ' + str(OP2) + ' = ',(num2words((int(OP1) + int(OP2)))).replace('-',' '))
    else:
        OP1 = random.randrange(1,100)
        OP2= random.randrange(0,OP1)
        makeQuestion('Fill in the missing word: The DIFFERENCE of ' + str(OP1) + ' and ' + str(OP2) + ' = ',(num2words((int(OP1) - int(OP2)))).replace('-',' '))
        
def makeQuestion(rawquestion, rawanswer):
    print(rawquestion)
    print(rawanswer)
    splitanswer = rawanswer.split(' ');
    potentialanswer = random.choice(splitanswer)
    if 'and' not in potentialanswer:
        finalanswer = potentialanswer
        fillblank = ''.ljust(len(potentialanswer),'*')
        rawanswer = rawanswer.replace(potentialanswer,fillblank)
        global counter;
        counter += 1
        writter.writerows([
                ('qid'+str(counter),rawquestion + rawanswer, finalanswer, str(current_timestamp), 'www.cleancaptcha.com'),
                ])
        print(rawquestion + rawanswer, finalanswer)
        

for i in xrange(0,1000):
    getRandoms(i)