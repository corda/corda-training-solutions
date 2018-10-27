//extra long factorials
//hackerrank solution in c++

#include <math.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <assert.h>
#include <limits.h>
#include <stdbool.h>

int mult(int size,int res[],int x)
    {
    int carry=0,prod;
    for(int j=0;j<size;j++)
        {
        prod=res[j]*x+carry;
        res[j]=prod%10;
        carry=prod/10;
    }
    while(carry)
        {
        res[size]=carry%10;
        carry=carry/10;
        size++;
    }
    return size;
}
void fact(int n)
    {
    int i,size;
    int res[200];
    res[0]=1;
    size=1;
    for(i=2;i<=n;i++)
        {
        size=mult(size,res,i);
    }
    for(i=size-1;i>=0;i--)
        printf("%d",res[i]);
    
}
int main() {
int n;
    scanf("%d",&n);
    fact(n);
    /* Enter your code here. Read input from STDIN. Print output to STDOUT */    
    return 0;
}
