package edu.duke.ece568.amazon;

public class SeqnumFactory {
  private long seqnum;
  
  public SeqnumFactory() {
    seqnum = 1;
  }

  public synchronized long createSeqnum() {
    return seqnum++;
  }
}