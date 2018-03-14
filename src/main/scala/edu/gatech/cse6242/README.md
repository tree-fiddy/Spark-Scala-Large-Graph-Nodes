# Total Node Gross Degree

## Dependencies
This program runs on Cloudera's CDH 5.18 distribution.  It also requires a few packages (which I haven't looked into... I just received them and went along with it).
## Objective
Using a TSV with 3 columns (Source, Target, and Weight), calculate TOTAL out-degree and in-degree of nodes.  Then, find the Gross difference per node

## Instructions
 
 
Source   |    Target|     weight
:---:| :---:    | :---:
1  |    2   |   40
2  |    3   |   100
1  |    3   |   60
3  |    4   |   1
3  |    1   |   10

Results in a final output file of:  

Node # | Difference
:---:| :---:
-1     |   2

0        1

2        1

#### To Test on smaller toy dataset
Fork or clone, and `cd ` into Q4.  Then, run the following commands in CDH 5.18:
>  mvn package  
>  bash run.sh

That's it.

#### On Azure
Install Azure CLI.  

Open a command prompt, bash, or other shell, and use az login command to authenticate to your Azure subscription.  
When prompted, enter the username and password for your subscription.

`az storage account list` command will list the storage accounts for your subscription.
`az storage account keys list --account-name <storage-account-name> --resource-group <resource-group-name>` command should return Primary and Secondary keys. Copy the Primary key value because it will be used in the next steps.  
`az storage container list --account-name <storage-account-name> --account-key <primary-key-value>` command will list your blob containers.  
`az storage blob upload --account-name <storage account name> --account-key <primary-key-value> --file <small or large .tsv> --container-name <container-name> --name <name for the new blob>/<small or large .tsv>` command will upload the source file to your blob storage container.

Upload data: 
`scp <your-relative-path>/q4-1.0.jar USERNAME@CLUSTERNAME-ssh.azurehdinsight.net:`
Replace USERNAME with your SSH user name for the cluster. Replace CLUSTERNAME with the HDInsight cluster name.

`ssh USERNAME@CLUSTERNAME-ssh.azurehdinsight.net`

`yarn jar q4-1.0.jar edu.gatech.cse6242.Q4 wasbs://<container-name>@<blob-storage-name>.blob.core.windows.net/<small-blob-name>/small.tsv wasbs://<container-name>@<blob-storage-name>.blob.core.windows.net/smalloutput` all in one command  
Note:  <container-name> is taken from `name` when running `az storage container list --account-name <storage-account-name>...`
Note:  <blob-storage-name> is taken from the storage container short-hand name.  

The output will be located in the `wasbs://<container-name>@<blob-storage-name>.blob.core.windows.net/smalloutput`. If there are multiple  output files, merge the files in this directory using the following command:

 

`hdfs dfs -cat wasbs://<container-name>@<blob-storage-name>.blob.core.windows.net/smalloutput/* > small.out`  

Command format: hdfs dfs -cat location/* >outputFile  
Note:  container-name is the long-version name and blob storage name here are the short-hand names  

Finally, exit out of the Azure SSH client and run:  
`scp <username>@<cluster-name>-ssh.azurehdinsight.net:/home/<username>/small.out .`
where "." references a local directory.  
<cluster-name> is the shorthand name of the HDInsight cluster.  

## Details
Calculates the count of out-degree - in-degree differences.  
For example, if the degree difference is -1, and this happens 10 times, then -1 will be associated with the count of 10.

