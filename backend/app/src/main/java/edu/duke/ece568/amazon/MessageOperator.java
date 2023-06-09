package edu.duke.ece568.amazon;

import com.google.protobuf.*;
import edu.duke.ece568.amazon.protocol.AmazonUps.*;
import edu.duke.ece568.amazon.protocol.WorldAmazon.*;
import java.io.*;
import java.sql.*;
import java.util.*;

public class MessageOperator {

  /**
   * This constructs a message operator
   */
  public MessageOperator() {}

  /**
   * This sends a goolge protocol buffer message through socket,
   * adjusts from the C++ version example provided in the project pdf
   */ 
  public <T extends GeneratedMessageV3> boolean sendMessage(T message, OutputStream output) throws IOException { 
    CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
    byte[] rawData = message.toByteArray();
    int size = rawData.length;
    codedOutput.writeUInt32NoTag(size); // writeRawVarint32() is deprecated, use writeUInt32NoTag() instead
    message.writeTo(codedOutput);
    codedOutput.flush();
    return true;
  }

  /**
   * This receives a goolge protocol buffer message through socket,
   * adjusts from the C++ version example provided in the project pdf
   */ 
  public <T extends GeneratedMessageV3.Builder<?>> boolean receiveMessage(T message, InputStream input) throws IOException {
    CodedInputStream codedInput = CodedInputStream.newInstance(input);
    int size = codedInput.readRawVarint32();
    int limit = codedInput.pushLimit(size);
    message.mergeFrom(codedInput);
    codedInput.popLimit(limit);
    return true; 
  }
  
}
