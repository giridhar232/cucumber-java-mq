// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/test/resources/AddressBook.proto

package com.gn.protobuf;

public interface AddressBookOrBuilder extends
    // @@protoc_insertion_point(interface_extends:protobuf.AddressBook)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .protobuf.Person people = 1;</code>
   */
  java.util.List<com.gn.protobuf.Person> 
      getPeopleList();
  /**
   * <code>repeated .protobuf.Person people = 1;</code>
   */
  com.gn.protobuf.Person getPeople(int index);
  /**
   * <code>repeated .protobuf.Person people = 1;</code>
   */
  int getPeopleCount();
  /**
   * <code>repeated .protobuf.Person people = 1;</code>
   */
  java.util.List<? extends com.gn.protobuf.PersonOrBuilder> 
      getPeopleOrBuilderList();
  /**
   * <code>repeated .protobuf.Person people = 1;</code>
   */
  com.gn.protobuf.PersonOrBuilder getPeopleOrBuilder(
      int index);
}