package com.campusassist.model;

/** The fixed list of complaint categories. An enum stops typos like "Hostl" from ever reaching the database. */
public enum Category {
    ACADEMIC, HOSTEL, FEES, TRANSPORT, LIBRARY, INFRASTRUCTURE, OTHER
}
