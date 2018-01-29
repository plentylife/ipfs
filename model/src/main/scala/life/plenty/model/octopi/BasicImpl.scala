package life.plenty.model.octopi

trait Question extends Space with WithParent[Space] {}

class BasicQuestion() extends Question {}

class BasicSpace() extends Space with WithMembers {
}