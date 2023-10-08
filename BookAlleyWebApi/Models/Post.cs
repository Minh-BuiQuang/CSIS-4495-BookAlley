namespace BookAlleyWebApi.Models
{
    public class Post
    {
        public long Id { get; set; }
        public required User Poster { get; set; }
        public required string Title { get; set; }
        public required string Author { get; set; }
        public required string ISBN { get; set; }
        public required string BookTitle { get; set; }
        public required string Image { get; set; }
        public required string Location { get; set; }
        public required string DatePosted { get; set; }
    }
}
