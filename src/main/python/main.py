import pandas as pd

from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import SGDClassifier
from sklearn.naive_bayes import MultinomialNB

from KaggleWord2VecUtility import KaggleWord2VecUtility

train_file = 'data/labeledTrainData.tsv'
unlabeled_train_file = 'data/unlabeledTrainData.tsv'
test_file = 'data/testData.tsv'
output_file = 'data/submit_200_4.csv'

train = pd.read_csv( train_file, header = 0, delimiter = "\t", quoting = 3 )
test = pd.read_csv( test_file, header = 0, delimiter = "\t", quoting = 3 )
unlabeled_train = pd.read_csv( unlabeled_train_file, header = 0, delimiter= "\t", quoting = 3 )

print "Parsing train reviews..."

clean_train_reviews = []
for review in train['review']:
    clean_train_reviews.append( " ".join( KaggleWord2VecUtility.review_to_wordlist( review )))

unlabeled_clean_train_reviews = []
for review in unlabeled_train['review']:
    unlabeled_clean_train_reviews.append( " ".join( KaggleWord2VecUtility.review_to_wordlist( review )))

print "Parsing test reviews..."

clean_test_reviews = []
for review in test['review']:
    clean_test_reviews.append( " ".join( KaggleWord2VecUtility.review_to_wordlist( review )))

print "Vectorizing..."

vectorizer = TfidfVectorizer( min_df=2, max_df=0.95, max_features = 200000, ngram_range = ( 1, 4 ),
                              sublinear_tf = True )

vectorizer = vectorizer.fit(clean_train_reviews + unlabeled_clean_train_reviews)
train_data_features = vectorizer.transform( clean_train_reviews )
test_data_features = vectorizer.transform( clean_test_reviews )

print "Reducing dimension..."

from sklearn.feature_selection.univariate_selection import SelectKBest, chi2, f_classif
fselect = SelectKBest(chi2 , k=70000)
train_data_features = fselect.fit_transform(train_data_features, train["sentiment"])
test_data_features = fselect.transform(test_data_features)

print "Training..."

model1 = MultinomialNB(alpha=0.0005)
model1.fit( train_data_features, train["sentiment"] )

model2 = SGDClassifier(loss='modified_huber', n_iter=5, random_state=0, shuffle=True)
model2.fit( train_data_features, train["sentiment"] )

p1 = model1.predict_proba( test_data_features )[:,1]
p2 = model2.predict_proba( test_data_features )[:,1]

print "Writing results..."

output = pd.DataFrame( data = { "id": test["id"], "sentiment": .2*p1 + 1.*p2 } )
output.to_csv( output_file, index = False, quoting = 3 )

