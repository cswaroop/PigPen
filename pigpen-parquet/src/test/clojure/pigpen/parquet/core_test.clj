;;
;;
;;  Copyright 2013 Netflix, Inc.
;;
;;     Licensed under the Apache License, Version 2.0 (the "License");
;;     you may not use this file except in compliance with the License.
;;     You may obtain a copy of the License at
;;
;;         http://www.apache.org/licenses/LICENSE-2.0
;;
;;     Unless required by applicable law or agreed to in writing, software
;;     distributed under the License is distributed on an "AS IS" BASIS,
;;     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;     See the License for the specific language governing permissions and
;;     limitations under the License.
;;
;;

(ns pigpen.parquet.core-test
  (:require [clojure.test :refer :all]
            [pigpen.extensions.test :refer [test-diff pigsym-zero pigsym-inc regex->string]]
            [pigpen.parquet.core :as pig-parquet]))

(deftest test-load-parquet
  (with-redefs [pigpen.raw/pigsym (pigsym-inc)]
    (test-diff
      (pig-parquet/load-parquet "foo" {:x :chararray, :y :int})
      '{:type :bind
        :id bind2
        :description nil
        :func (pigpen.pig/map->bind (pigpen.pig/args->map pigpen.pig/native->clojure))
        :args ["y" y "x" x]
        :requires []
        :fields [value]
        :field-type-in :native
        :field-type-out :frozen
        :opts {:type :bind-opts}
        :ancestors [{:type :load
                     :id load1
                     :description "foo"
                     :location "foo"
                     :fields [y x]
                     :field-type :native
                     :storage {:type :storage
                               :references []
                               :func "parquet.pig.ParquetLoader"
                               :args ["y:int,x:chararray"]}
                     :opts {:type :load-opts
                            :implicit-schema true}}]})))

(deftest test-store-parquet
  (with-redefs [pigpen.raw/pigsym (pigsym-inc)]
    (test-diff
      (pig-parquet/store-parquet "foo" {:x :chararray, :y :int} {:fields '[value]})
      '{:type :store
        :id store3
        :description "foo"
        :location "foo"
        :fields [y x]
        :storage {:type :storage
                  :references []
                  :func "parquet.pig.ParquetStorer"
                  :args []}
        :opts {:type :store-opts
               :schema {:y :int, :x :chararray}}
        :ancestors [{:projections [{:type :projection-field
                                    :field 0
                                    :alias y}
                                   {:type :projection-field
                                    :field 1
                                    :alias x}]
                     :fields [y x]
                     :ancestors [{:type :bind
                                  :id bind1
                                  :description nil
                                  :func (pigpen.pig/keyword-field-selector->bind [:y :x])
                                  :args [value]
                                  :requires []
                                  :fields [value]
                                  :field-type-in :frozen
                                  :field-type-out :native
                                  :opts {:type :bind-opts
                                         :implicit-schema true}
                                  :ancestors [{:fields [value]}]}]
                     :type :generate
                     :id generate2
                     :description nil
                     :field-type :native
                     :opts {:type :generate-opts}}]})))
