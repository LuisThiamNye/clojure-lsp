(ns clojure-lsp.shared-test
  (:require
   [clojure-lsp.db :as db]
   [clojure-lsp.shared :as shared]
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing]]))

(def windows? (clojure.string/starts-with? (System/getProperty "os.name") "Windows"))

(defn file-path [path]
  (cond-> path windows?
          (-> (string/replace "^/" "c:\\")
              (string/replace "/" "\\"))))

(deftest filename->uri
  (testing "when it is not a jar"
    (reset! db/db {})
    (is (= "file:///some%20project/foo/bar_baz.clj"
           (shared/filename->uri "/some project/foo/bar_baz.clj"))))
  (testing "when it is a jar via zipfile"
    (reset! db/db {})
    (is (= "zipfile:///home/some/.m2/some-jar.jar::clojure/core.clj"
           (shared/filename->uri "/home/some/.m2/some-jar.jar:clojure/core.clj"))))
  (testing "when it is a jar via jarfile"
    (reset! db/db {:settings {:dependency-scheme "jar"}})
    (is (= "jar:file:///home/some/.m2/some-jar.jar!/clojure/core.clj"
           (shared/filename->uri "/home/some/.m2/some-jar.jar:clojure/core.clj"))))
  (testing "Windows URIs"
    (is (= "file:///c:/c.clj"
           (shared/filename->uri "c:\\c.clj")))))

(deftest uri->filename
  (testing "should decode special characters in file URI"
    (is (= (file-path "/path+/encoded characters!")
           (shared/uri->filename "file:///path%2B/encoded%20characters%21"))))
  (testing "when it is a jar via zipfile"
    (is (= (file-path "/something.jar:something/file.cljc")
           (shared/uri->filename "zipfile:///something.jar::something/file.cljc"))))
  (testing "when it is a jar via zipfile with encoding"
    (is (= (file-path "/something.jar:something/file.cljc")
           (shared/uri->filename "zipfile:///something.jar%3A%3Asomething/file.cljc"))))
  (testing "when it is a jar via jarfile"
    (is (= (str (file-path "/Users/clojure-1.9.0.jar") ":clojure/string.clj")
           (shared/uri->filename "jar:file:///Users/clojure-1.9.0.jar!/clojure/string.clj"))))
  (testing "Windows URIs"
    (is (= (when windows? "c:\\c.clj")
           (when windows? (shared/uri->filename "file:/c:/c.clj"))))
    (is (= (when windows? "c:\\c.clj")
           (when windows? (shared/uri->filename "file:///c:/c.clj"))))))

(deftest relativize-filepath
  (is (= (file-path "some/path.clj")
         (shared/uri->relative-filepath
          (file-path "/User/rich/some/path.clj")
          (file-path "/User/rich")))))

(deftest uri->relative-filepath
  (is (= (file-path "some /path.clj")
         (shared/uri->relative-filepath "file:///User/rich%20/some%20/path.clj" "file:///User/rich%20"))))

(deftest join-filepaths
  (is (= (file-path "/users/melon/toasty/onion")
         (if windows?
           (shared/join-filepaths "c:\\users" "/" "melon\\toasty" "onion")
           (shared/join-filepaths "/users" "/" "melon/toasty" "onion")))))

(deftest ->range-test
  (testing "should subtract 1 from row and col values"
    (is (= {:start {:line      1
                    :character 1}
            :end   {:line      1
                    :character 1}}
           (shared/->range {:row 2 :end-row 2 :col 2 :end-col 2}))))
  (testing "should not return negative line and character values"
    (is (= {:start {:line      0
                    :character 0}
            :end   {:line      0
                    :character 0}}
           (shared/->range {:row 0 :end-row 0 :col 0 :end-col 0})))))
