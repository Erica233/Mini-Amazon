package edu.duke.ece568.amazon;

public class SeqnumFactory {

  /**
   * This is the sequence number
   */
  private long seqnum;
  
  /**
   * This constructs a sequence number factory, in which
   * the sequence number starts from 1
   */
  public SeqnumFactory() {
    seqnum = 1;
  }

  /**
   * This returns the current sequence number, and then
   * increase the sequence number by 1
   */
  public synchronized long createSeqnum() {
    return seqnum++;
  }
}