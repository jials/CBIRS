# CBIRS
Content-Based Image Retrieval System

## Objective ##
This assignment aims to implement a program to index, match and retrieve images based on a range of visual, concept and text features. In particular, you will explore color histogram (CH), visual keywords (VW), visual concepts (VC) and text (TEXT) features for retrieval. You will be given tools (APIs) to extract color histogram, visual keywords and visual concepts from an input image. You need to design and implement a program to fuse these features, including text, to perform the equivalence of concept-based image retrieval.

## TO-DO ##
#### Feature Extraction 
- Extract the necessary features, i.e. CH, VW, VC and TEXT.

#### Similarity Matching 
- Design and test several similarity measures and fusion function to derive the similarity between two images based on the above   set of features.
- A simple UI should be developed to present the ranked list of results, and if necessary, allow the users to perform relevance   feedback. Your UI must also allow users to try the use of different combinations of features.

#### Analysis and Aggregation of Results 
- You are to systematically test the features and analyze your results to draw conclusions on the effectiveness of various    features and techniques. For this, I expect you to generate average F1 or MAP based on the top 20 returned results.
- You need to test several configurations of system, including the use of only individual feature {CH, VW, VC or TEXT}, various   combinations of features, and full feature set.
- You need to analyze the results to draw conclusions on the effectiveness of various features with respect to different types    of queries and different categories of images.
