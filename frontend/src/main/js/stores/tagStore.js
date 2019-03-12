import { observable, action } from "mobx";

class TagStore {
    @observable store = {
        categories: []
    };

    @observable editTag = {
        contents: "",
        category: "",
        source: "",
        contentOptions: []
    };

    @observable editCtg = {
        category: "",
        source: "",
        id: null
    };

    @action setEditedTagContents(contents) {
        this.editTag.contents = contents;
    }

    @action setEditedTagContentOptions(contentOptions) {
        this.editTag.contentOptions = contentOptions;
    }

    @action setEditedTagCtg(category) {
        this.store.categories.map(c => {
            if (c.category === category) {
                this.editTag.category = c.category;
                this.editTag.source = c.source;
            }
        });
    }

    @action setEditedCtg(ctg, id) {
        this.editCtg.category = ctg;
        this.editCtg.id = id;
    }

    @action setEditedSource(src, id) {
        this.editCtg.source = src;
        this.editCtg.id = id;
    }

    @action
    updateCategories(ctg) {
        this.store.categories = ctg;
    }
}
export default new TagStore();
