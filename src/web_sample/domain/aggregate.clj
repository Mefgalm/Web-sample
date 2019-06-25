(ns web-sample.aggregate
  (:require [web-sample.common :refer :all]
            [web-sample.domain.common :refer :all]
            [web-sample.domain.user :as dom-user]))

; CREATE TABLE Event
; (SequenceNum bigserial NOT NULL
;              StreamId uuid NOT NULL
;              Data jsonb NOT NULL
;              Type text NOT NULL
;              Meta jsonb NOT NULL
;              LogDate timestamptz NOT NULL DEFAULT now ()
;              PRIMARY KEY (SequenceNum));

; CREATE INDEX idx_event_streamid ON Event (StreamId);
